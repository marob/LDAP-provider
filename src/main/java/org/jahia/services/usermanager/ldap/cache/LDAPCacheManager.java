/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.usermanager.ldap.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.cache.ModuleClassLoaderAwareCacheEntry;
import org.jahia.services.cache.ehcache.EhCacheProvider;

/**
 * Helper class for LDAP provider related caches.
 * 
 * @author kevan
 */
public class LDAPCacheManager {
    public static final String LDAP_USER_CACHE = "LDAPUsersCache";
    public static final String LDAP_GROUP_CACHE = "LDAPGroupsCache";

    private Ehcache groupCache;
    private Ehcache userCache;
    private EhCacheProvider cacheProvider;

    void start(){
        final CacheManager cacheManager = cacheProvider.getCacheManager();
        userCache = cacheManager.getCache(LDAP_USER_CACHE);
        if (userCache == null) {
            cacheManager.addCache(LDAP_USER_CACHE);
            userCache = cacheManager.getCache(LDAP_USER_CACHE);
        } else {
            userCache.removeAll();
        }
        groupCache = cacheManager.getCache(LDAP_GROUP_CACHE);
        if (groupCache == null) {
            cacheManager.addCache(LDAP_GROUP_CACHE);
            groupCache = cacheManager.getCache(LDAP_GROUP_CACHE);
        } else  {
            groupCache.removeAll();
        }
    }

    void stop(){
        // flush
        userCache.removeAll();
        groupCache.removeAll();
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public LDAPUserCacheEntry getUserCacheEntryByName(String providerKey, String username) {
        return (LDAPUserCacheEntry) CacheHelper.getObjectValue(userCache, getCacheNameKey(providerKey, username));
    }

    public LDAPUserCacheEntry getUserCacheEntryByDn(String providerKey, String dn) {
        return (LDAPUserCacheEntry) CacheHelper.getObjectValue(userCache, getCacheDnKey(providerKey, dn));
    }

    public void cacheUser(String providerKey, LDAPUserCacheEntry ldapUserCacheEntry) {
        ModuleClassLoaderAwareCacheEntry cacheEntry = new ModuleClassLoaderAwareCacheEntry(ldapUserCacheEntry, "ldap");
        userCache.put(new Element(getCacheNameKey(providerKey, ldapUserCacheEntry.getName()), cacheEntry));
        userCache.put(new Element(getCacheDnKey(providerKey, ldapUserCacheEntry.getDn()), cacheEntry));
    }

    public LDAPGroupCacheEntry getGroupCacheEntryName(String providerKey, String groupname) {
        return (LDAPGroupCacheEntry) CacheHelper.getObjectValue(groupCache, getCacheNameKey(providerKey, groupname));
    }

    public LDAPGroupCacheEntry getGroupCacheEntryByDn(String providerKey, String dn) {
        return (LDAPGroupCacheEntry) CacheHelper.getObjectValue(groupCache, getCacheDnKey(providerKey, dn));
    }

    public void cacheGroup(String providerKey, LDAPGroupCacheEntry ldapGroupCacheEntry) {
        ModuleClassLoaderAwareCacheEntry cacheEntry = new ModuleClassLoaderAwareCacheEntry(ldapGroupCacheEntry, "ldap");
        groupCache.put(new Element(getCacheNameKey(providerKey, ldapGroupCacheEntry.getName()), cacheEntry));
        groupCache.put(new Element(getCacheDnKey(providerKey, ldapGroupCacheEntry.getDn()), cacheEntry));
    }

    private String getCacheNameKey(String providerKey, String objectName) {
        return providerKey + "n" + objectName;
    }

    private String getCacheDnKey(String providerKey, String objectName) {
        return providerKey + "d" + objectName;
    }
}
