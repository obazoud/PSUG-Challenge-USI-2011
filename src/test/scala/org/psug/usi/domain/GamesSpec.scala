package org.psug.usi.domain

/**
 * User: alag
 * Date: 2/17/11
 * Time: 12:19 AM
 */

import actors.Actor._
import org.specs._
import java.util.concurrent.atomic.AtomicInteger
class GamesSpec extends SpecificationWithJUnit {
  import InMemoryGameRepository._
  def clearRepository = InMemoryGameRepository.reset

  "in-memory game repository" should { clearRepository.before

     
    val game = Game( questions = Question( "Q1", Answer( "A1", false )::Answer("A2", false)::Nil ) :: Nil )

    "assign unique id to user when registering" in {
      val DataStored( Right( gameStored ) ) = InMemoryGameRepository !? InMemoryGameRepository.StoreData(game)
      gameStored.id must be_!=( game.id )

    }


    "lookup game by id" in {

      val DataStored( Right( gameStored ) ) = InMemoryGameRepository !? StoreData(game)
      val DataPulled( Some( gameFound ) ) = InMemoryGameRepository !? PullData(gameStored.id)
      gameFound.questions.head.question must be_==( game.questions.head.question )

    }
  }


  "game manager" should { clearRepository.before

    val game = Game( questions = Question( "Q1", Answer( "A11", false )::Answer("A12", false)::Nil )
                                  :: Question( "Q2", Answer( "A21", false )::Answer("A22", false)::Nil )
                                  :: Nil,
                     timeoutSec = 10,
                     numPlayer = 1000 )

    val users = for( i <- 0 until game.numPlayer ) yield User( i, "firstName"+i, "lastName"+i, "email"+i, "password"+i )


    "wait for game numPlayer and send questions (and scoreSlice after 1st answer)" in {
      var currentQuestion = 0

      val playerAckCount = new AtomicInteger(0)

      // note we added self.loop & react because !? method used in prev test create un "instance" actor and endpoint loop might refer this this one
      val endpoint = actor {
        self.loop {
          self.react {
            case UserQuestion( userId, Some( question ), scoreSlice ) =>
              question must be_==( game.questions(currentQuestion) )
              if( currentQuestion > 0 ){
                val Some( userScores ) = scoreSlice
                userScores.find( _.userId == userId ) must notBe( None )
              }
              playerAckCount.incrementAndGet
            case _ => fail("Unexpected message => must alway have a question")
          }
        }
      }

      val gameManager = new GameManager( game )

      // 1st question
      users.foreach( user => gameManager.send( Register( user.id ), endpoint ) )
      while( playerAckCount.get < game.numPlayer ) Thread.sleep(10)


      // 2nd question
      playerAckCount.set(0)
      currentQuestion += 1
      users.foreach( user => gameManager.send( UserAnswer( user.id, currentQuestion, user.id%2 ), endpoint ) )


    }

  }
}
