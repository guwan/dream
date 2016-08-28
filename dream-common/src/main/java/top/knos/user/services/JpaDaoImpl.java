/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.knos.user.services;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import top.knos.user.Authority;
import top.knos.user.User;



/**
 * <tt>UserDetailsServiceRetrieves</tt> implementation which retrieves the user details
 * (username, password, enabled flag, and authorities) from a database using JDBC queries.
 *
 * <h3>Default Schema</h3>
 * A default database schema is assumed, with two tables "users" and "authorities".
 *
 * <h4>The Users table</h4>
 *
 * This table contains the login name, password and enabled status of the user.
 *
 * <table>
 * <tr><th>Column</th></tr>
 * <tr><td>username</td></tr>
 * <tr><td>password</td></tr>
 * <tr><td>enabled</td></tr>
 * </table>
 *
 * <h4>The Authorities Table</h4>
 *
 * <table>
 * <tr><th>Column</th></tr>
 * <tr><td>username</td></tr>
 * <tr><td>authority</td></tr>
 * </table>
 *
 * If you are using an existing schema you will have to set the queries <tt>usersByUsernameQuery</tt> and
 * <tt>authoritiesByUsernameQuery</tt> to match your database setup
 * (see {@link #DEF_USERS_BY_USERNAME_QUERY} and {@link #DEF_AUTHORITIES_BY_USERNAME_QUERY}).
 *
 * <p>
 * In order to minimise backward compatibility issues, this implementation doesn't recognise the expiration of user
 * accounts or the expiration of user credentials. However, it does recognise and honour the user enabled/disabled
 * column. This should map to a <tt>boolean</tt> type in the result set (the SQL type will depend on the
 * database you are using). All the other columns map to <tt>String</tt>s.
 *
 * <h3>Group Support</h3>
 * Support for group-based authorities can be enabled by setting the <tt>enableGroups</tt> property to <tt>true</tt>
 * (you may also then wish to set <tt>enableAuthorities</tt> to <tt>false</tt> to disable loading of authorities
 * directly). With this approach, authorities are allocated to groups and a user's authorities are determined based
 * on the groups they are a member of. The net result is the same (a UserDetails containing a set of
 * <tt>GrantedAuthority</tt>s is loaded), but the different persistence strategy may be more suitable for the
 * administration of some applications.
 * <p>
 * When groups are being used, the tables "groups", "group_members" and "group_authorities" are used. See
 * {@link #DEF_GROUP_AUTHORITIES_BY_USERNAME_QUERY} for the default query which is used to load the group authorities.
 * Again you can customize this by setting the <tt>groupAuthoritiesByUsernameQuery</tt> property, but the format of
 * the rows returned should match the default.
 *
 * @author Ben Alex
 * @author colin sampaleanu
 * @author Luke Taylor
 */
public class JpaDaoImpl extends JpaDaoSupport implements UserDetailsService {
    //~ Static fields/initializers =====================================================================================

    public static final String DEF_USERS_BY_USERNAME_QUERY ="select u from User u where u.username = ?1";
    public static final String DEF_USERS_BY_EMAIL_QUERY ="select u from User u where u.email = ?1";

    public static final String DEF_AUTHORITIES_BY_USERNAME_QUERY ="select a from Authority a where a.username = ?1";

    //~ Instance fields ================================================================================================

    protected final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private String authoritiesByUsernameQuery;
    private String usersByUsernameQuery;
    private String usersByEmailQuery;
    private String rolePrefix = "";
    private boolean usernameBasedPrimaryKey = true;

    //~ Constructors ===================================================================================================

    public JpaDaoImpl() {
        usersByUsernameQuery = DEF_USERS_BY_USERNAME_QUERY;
        usersByEmailQuery = DEF_USERS_BY_EMAIL_QUERY;
        authoritiesByUsernameQuery = DEF_AUTHORITIES_BY_USERNAME_QUERY;
    }

    //~ Methods ========================================================================================================

    /**
     * Allows subclasses to add their own granted authorities to the list to be returned in the <tt>UserDetails</tt>.
     *
     * @param username the username, for use by finder methods
     * @param authorities the current granted authorities, as populated from the <code>authoritiesByUsername</code>
     *        mapping
     */
    protected void addCustomAuthorities(String username, List<GrantedAuthority> authorities) {}


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	TypedQuery<User> tq= em.createQuery(usersByUsernameQuery,User.class).setParameter(1, username);
    	UserDetails user = tq.getSingleResult();
        return fillAuthority(user);
    }
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
    	TypedQuery<User> tq= em.createQuery(usersByEmailQuery,User.class).setParameter(1, email);
    	UserDetails user = tq.getSingleResult();
        return fillAuthority(user);
    }
    private UserDetails fillAuthority(UserDetails user){
    	TypedQuery<Authority> atq= em.createQuery(authoritiesByUsernameQuery,Authority.class).setParameter(1, user.getUsername());
    	
    	List<Authority> Authorities = atq.getResultList();
    	List<SimpleGrantedAuthority> sgal =new ArrayList<SimpleGrantedAuthority>();
    	for(Authority a : Authorities){
    		sgal.add(new SimpleGrantedAuthority(a.getAuthority()));
    	}
    	User domain =(User) user;
    	domain.setAuthorities(sgal);
    	return user;
    }

    /**
     * Allows the default query string used to retrieve authorities based on username to be overridden, if
     * default table or column names need to be changed. The default query is {@link
     * #DEF_AUTHORITIES_BY_USERNAME_QUERY}; when modifying this query, ensure that all returned columns are mapped
     * back to the same column names as in the default query.
     *
     * @param queryString The SQL query string to set
     */
    public void setAuthoritiesByUsernameQuery(String queryString) {
        authoritiesByUsernameQuery = queryString;
    }

    protected String getAuthoritiesByUsernameQuery() {
        return authoritiesByUsernameQuery;
    }



    /**
     * Allows a default role prefix to be specified. If this is set to a non-empty value, then it is
     * automatically prepended to any roles read in from the db. This may for example be used to add the
     * <tt>ROLE_</tt> prefix expected to exist in role names (by default) by some other Spring Security
     * classes, in the case that the prefix is not already present in the db.
     *
     * @param rolePrefix the new prefix
     */
    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    protected String getRolePrefix() {
        return rolePrefix;
    }

    /**
     * If <code>true</code> (the default), indicates the {@link #getUsersByUsernameQuery()} returns a username
     * in response to a query. If <code>false</code>, indicates that a primary key is used instead. If set to
     * <code>true</code>, the class will use the database-derived username in the returned <code>UserDetails</code>.
     * If <code>false</code>, the class will use the {@link #loadUserByUsername(String)} derived username in the
     * returned <code>UserDetails</code>.
     *
     * @param usernameBasedPrimaryKey <code>true</code> if the mapping queries return the username <code>String</code>,
     *        or <code>false</code> if the mapping returns a database primary key.
     */
    public void setUsernameBasedPrimaryKey(boolean usernameBasedPrimaryKey) {
        this.usernameBasedPrimaryKey = usernameBasedPrimaryKey;
    }

    protected boolean isUsernameBasedPrimaryKey() {
        return usernameBasedPrimaryKey;
    }

    /**
     * Allows the default query string used to retrieve users based on username to be overridden, if default
     * table or column names need to be changed. The default query is {@link #DEF_USERS_BY_USERNAME_QUERY}; when
     * modifying this query, ensure that all returned columns are mapped back to the same column names as in the
     * default query. If the 'enabled' column does not exist in the source database, a permanent true value for this
     * column may be returned by using a query similar to
     * <pre>
     * "select username,password,'true' as enabled from users where username = ?"
     * </pre>
     *
     * @param usersByUsernameQueryString The query string to set
     */
    public void setUsersByUsernameQuery(String usersByUsernameQueryString) {
        this.usersByUsernameQuery = usersByUsernameQueryString;
    }

    public String getUsersByUsernameQuery() {
        return usersByUsernameQuery;
    }
   
}
