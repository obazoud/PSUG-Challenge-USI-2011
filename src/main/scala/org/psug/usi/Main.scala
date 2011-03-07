package org.psug.usi

import com.sun.jersey.spi.container.servlet.ServletContainer

import org.psug.usi.netty.WebServer

/**
 * 
 * @author abailly@oqube.com
 * @version $Rev$
 */
object Main {

  val DEFAULT_PORT = "8082"

  def main(args : Array[String]) = {
    val port = if(args.length > 0) args(0) else DEFAULT_PORT

    val server = new WebServer(Integer.parseInt(port))
    
    println("Started PSUG USI2011 Challenge server at 0.0.0.0:" + port)
  }

}

class Main {

  var server : WebServer = null

  def start(args : String*) = {
    val port: Int = Integer.parseInt(args(1))
    args(0) match {
      case "Web" =>
        server = new WebServer(port)
        server.start
        println("Started PSUG USI2011 Challenge server at 0.0.0.0:" + port)
      case "Service" =>

    }
  }

  def stop() = server.stop

}