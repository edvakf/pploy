package models

import java.util
import javax.naming.Context
import javax.naming.directory.SearchControls
import javax.naming.ldap.InitialLdapContext

import scala.collection.mutable.ListBuffer

class LdapSearcher(url: String, user: String, pass: String) {
  val env = new util.Hashtable[String, String]()
  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
  env.put(Context.PROVIDER_URL, url)
  env.put(Context.SECURITY_PRINCIPAL, user)
  env.put(Context.SECURITY_CREDENTIALS, pass)

  // http://stackoverflow.com/questions/2172831/how-do-a-ldap-search-authenticate-against-this-ldap-in-java
  def search(where: String) = {
    val ctx = new InitialLdapContext(env, null)
    ctx.setRequestControls(null)
    val namingEnum = ctx.search(where, "cn=*", getSimpleSearchControls)
    val users = new ListBuffer[String]
    while (namingEnum.hasMore) {
      val attr = namingEnum.next().getAttributes.get("cn")
      if (attr.size > 0) users += attr.get(0).toString
    }
    namingEnum.close()
    users.toSeq
  }

  private def getSimpleSearchControls = {
    val searchControls = new SearchControls()
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE)
    searchControls.setTimeLimit(30000)
    searchControls
  }
}
