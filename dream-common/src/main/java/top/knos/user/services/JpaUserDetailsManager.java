package top.knos.user.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import top.knos.support.BCryptEncoder;
import top.knos.user.Authority;
import top.knos.user.AuthorityRepository;
import top.knos.user.User;
import top.knos.user.UserRepository;

/**
 * Jdbc user management service, based on the same table structure as its parent class, <tt>JdbcDaoImpl</tt>.
 * <p>
 * Provides CRUD operations for both users and groups. Note that if the {@link #setEnableAuthorities(boolean)
 * enableAuthorities} property is set to false, calls to createUser, updateUser and deleteUser will not store the
 * authorities from the <tt>UserDetails</tt> or delete authorities for the user. Since this class cannot differentiate
 * between authorities which were loaded for an individual or for a group of which the individual is a member,
 * it's important that you take this into account when using this implementation for managing your users.
 *
 * @author Luke Taylor
 * @since 2.0
 */
@Service
public class JpaUserDetailsManager extends JpaDaoImpl implements UserDetailsManager {
    //~ Instance fields ================================================================================================

    protected final Log logger = LogFactory.getLog(getClass());

    private AuthenticationManager authenticationManager;
	@Autowired UserRepository repository;
	@Autowired AuthorityRepository authorityRepository;
	@Autowired BCryptEncoder bcryptEncoder;
    private UserCache userCache = new NullUserCache();

    //~ Methods ========================================================================================================

    protected void initDao() throws ApplicationContextException {
        if (authenticationManager == null) {
            logger.info("No authentication manager set. Reauthentication of users when changing passwords will " +
                    "not be performed.");
        }
    }

    //~ UserDetailsManager implementation ==============================================================================

    @Override
    public void createUser(final UserDetails user) {
    	User reference =(User) user;
        validateUserDetails(reference);
        repository.save(reference);
        insertUserAuthorities(reference);
    }
    public void createUserWithDefaultAuth(final UserDetails user) {
    	User reference =(User) user;
        if(reference.getAuthorities()==null){
        	ArrayList<SimpleGrantedAuthority> al=new ArrayList<SimpleGrantedAuthority>();
        	al.add(new SimpleGrantedAuthority("ROLE_USER"));
        	reference.setAuthorities(al);
        }
        validateUserDetails(reference);
		reference.setPassword(bcryptEncoder.encode(reference.getPassword()));
        repository.save(reference);
        insertUserAuthorities(reference);
    }

    @Override
    public void updateUser(final UserDetails user) {
        validateUserDetails(user);
        repository.save((User)user);
        deleteUserAuthorities(user.getUsername());
        insertUserAuthorities((User) user);

        userCache.removeUserFromCache(user.getUsername());
    }

    @Override
    public void deleteUser(String username) {
    	deleteUserAuthorities(username);
        repository.deleteByUsername(username);
        userCache.removeUserFromCache(username);
    }
    

    @Override
    public void changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        newPassword = bcryptEncoder.encode(newPassword);
        if (currentUser == null) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException("Can't change password as no Authentication object found in context " +
                    "for current user.");
        }

        String username = currentUser.getName();

        // If an authentication manager has been set, re-authenticate the user with the supplied password.
        if (authenticationManager != null) {
            logger.debug("Reauthenticating user '"+ username + "' for password change request.");

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
        } else {
            logger.debug("No authentication manager set. Password won't be re-checked.");
        }

        logger.debug("Changing password for user '"+ username + "'");

        User reference =repository.findByUsername(username);
        reference.setPassword(newPassword);
        reference.setUpdateAt(new Date());
        repository.save(reference);
        
        //repository.setPasswordForUsername(newPassword,username);

        SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(currentUser, newPassword));

        userCache.removeUserFromCache(username);
    }
    public void resetPassword(String newPassword) throws AuthenticationException {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        newPassword = bcryptEncoder.encode(newPassword);
        if (currentUser == null) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException("Can't change password as no Authentication object found in context " +
                    "for current user.");
        }

        String username = currentUser.getName();

        logger.debug("Changing password for user '"+ username + "'");

        User reference =repository.findByUsername(username);
        reference.setPassword(newPassword);
        reference.setUpdateAt(new Date());
        repository.save(reference);
        
        //repository.setPasswordForUsername(newPassword,username);

        SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(currentUser, newPassword));

        userCache.removeUserFromCache(username);
    }

    protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
    	UserDetails user  = loadUserByUsername(currentAuth.getName());
        UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(user, newPassword, user.getAuthorities());
        newAuthentication.setDetails(currentAuth.getDetails());

        return newAuthentication;
    }

    @Override
    public boolean userExists(String username) {
    	User user =repository.findByUsername(username);
        return user == null;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Optionally sets the UserCache if one is in use in the application.
     * This allows the user to be removed from the cache after updates have taken place to avoid stale data.
     *
     * @param userCache the cache used by the AuthenticationManager.
     */
    public void setUserCache(UserCache userCache) {
        Assert.notNull(userCache, "userCache cannot be null");
        this.userCache = userCache;
    }

    private void validateUserDetails(UserDetails user) {
        Assert.hasText(user.getUsername(), "Username may not be empty or null");
        validateAuthorities(user.getAuthorities());
    }

    private void validateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Authorities list must not be null");

        for (GrantedAuthority authority : authorities) {
            Assert.notNull(authority, "Authorities list contains a null entry");
            Assert.hasText(authority.getAuthority(), "getAuthority() method must return a non-empty string");
        }
    }
	private void insertUserAuthorities(User reference) {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities=reference.getAuthorities();
        List<Authority> authorities=new ArrayList<Authority>();
        for(SimpleGrantedAuthority s:simpleGrantedAuthorities){
        	authorities.add(new Authority(reference.getUsername(), s.getAuthority()));
        }
        authorityRepository.save(authorities);
	}


	private void deleteUserAuthorities(String username) {
		authorityRepository.deleteByUsername(username);
	}
}
