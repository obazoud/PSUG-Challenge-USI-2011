package org.psug.usi.rest

/**
 * User: alag
 * Date: 2/22/11
 * Time: 1:28 AM
 */

import org.psug.usi.users._

import org.junit.Assert._
import org.junit.Test

import org.hamcrest.CoreMatchers._

import net.liftweb.json._

import com.sun.jersey.api.client._

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import org.psug.usi.netty.WebServer


class NettyUserRegistrationTest {
  implicit val formats = Serialization.formats(NoTypeHints)

  val martinOdersky = User( "Martin", "Odersky","m.odersky@scala-lang.org","0xcafebabe")
  val myriamOdersky = User("Myriam", "Odersky","m.odersky@scala-lang.org","0xbabecafe")

  WebServer.defaultWebServer
  
  // TODO need to test webserver on appropriate port
  @Test
  def succeedsIfUserDoesNotExist() = {
/*
    val response = webResource.path("/api/user/").header("Content-Type","application/json").post(classOf[String], write(martinOdersky))
    assertThat(response, is("1"))
    val user = webResource.path("/api/user/1").get(classOf[String])
    assertThat(read[User](user),is(martinOdersky))
    */
  }

  @Test
  def doesNotSucceedIfUserWithSameEmailExists() = {
    /*
    webResource.path("/api/user/").header("Content-Type","application/json").post(classOf[String], write(martinOdersky))
    try {
      val response = webResource.path("/api/user/").header("Content-Type","application/json").post(classOf[String], write(myriamOdersky))
    } catch {
      case e : UniformInterfaceException => assertThat(e.getResponse.getStatus,is(400))
    }
*/
  }


}