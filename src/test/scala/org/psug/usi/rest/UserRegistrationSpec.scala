package org.psug.usi.rest

/**
 * User: alag
 * Date: 2/22/11
 * Time: 1:28 AM
 */


import org.specs._

import com.sun.jersey.api.client._

import net.liftweb.json._
import net.liftweb.json.Serialization.read
import org.psug.usi.netty._
import org.psug.usi.service.SimpleRepositoryServices
import org.psug.usi.store.ClearRepository

import org.jboss.netty.handler.codec.http.Cookie
import org.jboss.netty.handler.codec.http.CookieDecoder

import org.junit.runner.RunWith
import org.specs.runner.JUnitSuiteRunner
import org.psug.usi.domain.{UserVO, User, AuthenticationToken, Credentials}

@RunWith(classOf[JUnitSuiteRunner])
class UserRegistrationSpec  extends SpecificationWithJUnit {

  implicit val formats = Serialization.formats(NoTypeHints)

  val martinOdersky = UserVO( "Martin", "Odersky","m.odersky@scala-lang.org","0xcafebabe")
  val myriamOdersky = UserVO("Myriam", "Odersky","m.odersky@scala-lang.org","0xbabecafe")
  val robertOdersky = UserVO("Robert", "Odersky","r.odersky@scala-lang.org","0xdeadbeef")

  val listenPort = 12345

  def webResource( path:String ) = new Client().resource("http://localhost:"+listenPort+path)

  new SpecContext {
    val repositories = new SimpleRepositoryServices
    val webServer : WebServer = new WebServer(listenPort,repositories)

    // launch/shutdown web server on each Specification
    beforeSpec { webServer.start; repositories.launch  }
    afterSpec { webServer.stop ; repositories.shutdown }

    // clear repository on each example


    before {
      repositories.userRepositoryService !? ClearRepository
    }


  }

  def registerUser(user : UserVO) : ClientResponse = {
     webResource("/api/user/").header("Content-Type","application/json").post(classOf[ClientResponse], Serialization.write(user))
  }

  def userLogsIn(credentials: Credentials) : ClientResponse = {
     webResource("/api/login/").header("Content-Type","application/json").post(classOf[ClientResponse], Serialization.write(credentials))
  }

  def getCookieFrom(response : ClientResponse) : Option[Cookie] = {
    Some(new CookieDecoder().decode(response.getHeaders.getFirst( "Set-Cookie")).iterator.next)
  }
  
/*
    // spec don't expect to have a user in response, just a code: OK : CREATED 201 Erreur : 400
  "user registration" should {
    shareVariables()


    "succeeds if user does not exist" in { 

      read[User](registerUser(martinOdersky)).id must be_==(1)

      val expectedUser = User( martinOdersky ).copyWithAutoGeneratedId( 1 )
      val user = read[User]( webResource("/api/user/1").get(classOf[String]) )
      user must be_==(expectedUser)

    }

    "fail if user with same mail exists" in {
      registerUser(martinOdersky)
      registerUser(myriamOdersky) must throwA[UniformInterfaceException]
    }
  }
*/


  "user login" should {
    shareVariables()

    "succeed with returned session cookie if user provide right credentials" in {
      registerUser(martinOdersky).getStatus must be_==(ClientResponse.Status.CREATED.getStatusCode)
      
      val response = userLogsIn(Credentials("m.odersky@scala-lang.org", "0xcafebabe"))
      response.getStatus must be_==(ClientResponse.Status.CREATED.getStatusCode)
      response.getHeaders.get("Set-Cookie").size must be_==(0).not
    }

    "fail if user does not provide right password" in {
      registerUser(martinOdersky).getStatus must be_==(ClientResponse.Status.CREATED.getStatusCode)
      val response = userLogsIn(Credentials("m.odersky@scala-lang.org", "0xcafebab"))
      response.getStatus must be_==(ClientResponse.Status.UNAUTHORIZED.getStatusCode)
      response.getHeaders.get("Set-Cookie") must beNull
    }

    "fail if user does not exists" in {
      registerUser(martinOdersky)
      val response = userLogsIn(Credentials("m.odersky@scala-lan.org", "0xcafebabe"))
      response.getStatus must be_==(ClientResponse.Status.UNAUTHORIZED.getStatusCode)
      response.getHeaders.get("Set-Cookie") must beNull
    }

    "encodes user mail and id in cookie" in {
      import AuthenticationToken._
      
      registerUser(martinOdersky)
      registerUser(robertOdersky)

      val Some(martinCookie) = getCookieFrom(userLogsIn(Credentials("m.odersky@scala-lang.org", "0xcafebabe")))
      martinCookie.getName must be_==("session_key")
      decrypt(martinCookie.getValue) must be_==(AuthenticationToken(1, "m.odersky@scala-lang.org"))
      val Some(robertCookie) = getCookieFrom(userLogsIn(Credentials("r.odersky@scala-lang.org", "0xdeadbeef")))
      decrypt(robertCookie.getValue) must be_==(AuthenticationToken(2, "r.odersky@scala-lang.org"))
    }
  }

}
