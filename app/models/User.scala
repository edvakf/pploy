package models

import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache

case class User(name: String) {
  if (!User.allNames.contains(name)) {
    throw new IllegalArgumentException("bad user name")
  }
}

object User {
  def allNames: Seq[String] = {
    current.configuration.getString("pploy.ldap.url").map { url =>
      // if url is present, assume others are present too

      val login = current.configuration.getString("pploy.ldap.login").get
      val password = current.configuration.getString("pploy.ldap.password").get
      val search = current.configuration.getString("pploy.ldap.search").get

      def fetchLdap = {
        Logger.info("fetching users from LDAP: " + search)
        new LdapSearcher(url, login, password).search(search)
      }

      current.configuration.getInt("pploy.ldap.cachettl").fold(fetchLdap) { ttl =>
        Cache.getAs[Seq[String]]("ldap_users").getOrElse {
          val names = fetchLdap
          Cache.set("ldap_users", names, ttl)
          names
        }
      }

    }.getOrElse(
      // otherwise get users from pploy.users
      current.configuration.getStringSeq("pploy.users").get
    )
  }
}
