package org.psug.usi.domain

/**
 * User: alag
 * Date: 2/17/11
 * Time: 12:19 AM
 */

import org.specs._
import org.psug.usi.service.SimpleRepositoryServices._
import org.psug.usi.store._
import org.psug.usi.service.{UserAnswer, Register, GameManagerService, UserAnswerResponse}
import actors.Futures
import scala.io.Source

class GamesSpec extends SpecificationWithJUnit {

  def clearRepository = gameRepositoryService.remoteRef ! ClearRepository

  "a game" should {
    "be definable using xml string" in {
      val game = Game( Source.fromFile( "./test-data/simplegamesession.xml" ).mkString )
      game.loginTimeoutSec must be_==( 3 )
      game.synchroTimeSec must be_==( 5 )
      game.questionTimeFrameSec must be_==( 11 )
      game.nbQuestions must be_==( 6 )
      game.nbUsersThreshold must be_==( 7 )
      game.flushUserTable must be_==( true )

      game.questions.size must be_==( 6 )
      0 to 4 foreach{ i => game.questions(i).value must be_==( 1 ) }
      game.questions( 5 ).value must be_==( 5 )

      game.questions.zipWithIndex.foreach{
        case( question, questionIndex ) =>
        question.question must be_==( "Q"+(questionIndex+1) )
        question.answers.zipWithIndex.foreach{
          case( answer, answserIndex ) =>
          answer.anwser must be_==( "A"+(questionIndex+1)+(answserIndex+1) )
          answer.status must be_==( questionIndex % 4 == answserIndex )  
        }
      }


    }
  }

  "in-memory game repository" should {
    clearRepository.before
     
    val game = Game( questions = Question( "Q1", Answer( "A1", false )::Answer("A2", false)::Nil, 1 ) :: Nil, nbQuestions = 1 )

    "assign unique id to user when registering" in {
      val DataStored( Right( gameStored ) ) = gameRepositoryService.remoteRef !? StoreData(game)
      gameStored.asInstanceOf[Game].id must be_!=( game.id )

    }

    "lookup game by id" in {

      val DataStored( Right( gameStored ) ) = gameRepositoryService.remoteRef !? StoreData(game)
      val DataPulled( Some( gameFound ) ) = gameRepositoryService.remoteRef !? PullData(gameStored.asInstanceOf[Game].id)
      gameFound.asInstanceOf[Game].questions.head.question must be_==( game.questions.head.question )

    }
  }

  "game manager" should {
    clearRepository.before

    val game = Game( questions = Question( "Q1", Answer( "A11", false )::Answer("A12", true)::Nil, 1 )
                    :: Question( "Q2", Answer( "A21", false )::Answer("A22", true)::Nil, 2 )
                    :: Nil,
                    loginTimeoutSec = 10,
                    synchroTimeSec = 10,
                    questionTimeFrameSec = 10,
                    nbQuestions = 2,
                    flushUserTable = false,
                    nbUsersThreshold = 100 )

    val users = for( i <- 0 until game.nbUsersThreshold ) yield User( i, "firstName"+i, "lastName"+i, "email"+i, "password"+i )


    "register all players, send question after each answer, send score slice and save user history after last response" in {


      val gameManager = new GameManagerService( game, gameUserHistoryService )

      var currentQuestion = 0

      // Register
      val futuresRegister = users.map( user => gameManager.remoteRef !! Register( user.id ) )
      /*
      Futures.awaitAll( 10000, futuresRegister.toSeq:_* ).foreach{
        case Some( userQuestion:UserAnswerResponse ) =>
          //val UserAnswerResponse( uid, Some( nextQuestion ), None, None, _ ) = userQuestion
          //nextQuestion must be_==( game.questions(currentQuestion ) )
        case _ => fail
      }

      // Answer question 0
      val futuresQ1 = users.map( user => gameManager.remoteRef !!  UserAnswer( user.id, currentQuestion, user.id%(game.questions(currentQuestion).answers.size) )  )
      Futures.awaitAll( 10000, futuresQ1.toSeq:_* ).foreach{
        case Some( userQuestion:UserAnswerResponse ) =>
          val UserAnswerResponse( anwserStatus, score ) = userQuestion

          val expectedScore = if( anwserStatus ) game.questions(currentQuestion).value else 0
          score must be_== ( expectedScore )

          //nextQuestion must be_==( game.questions( currentQuestion+1 ) )
        case _ => fail
      }

      currentQuestion += 1

      // Answer question 1
      val futuresQ2 = users.map( user => gameManager.remoteRef !!  UserAnswer( user.id, currentQuestion, user.id%(game.questions(currentQuestion).answers.size) )  )
      Futures.awaitAll( 10000, futuresQ2.toSeq:_* ).foreach{
        case Some( userQuestion:UserAnswerResponse ) =>
          val UserAnswerResponse( anwserStatus, score ) = userQuestion
/*
          val expectedPrevScoreWithBonus = if( game.questions(currentQuestion).answers( uid%(game.questions(currentQuestion-1).answers.size) ).status  ) game.questions(currentQuestion-1).value+1 else 0
          val expectedScore = if( anwserStatus ) game.questions(currentQuestion).value+expectedPrevScoreWithBonus else expectedPrevScoreWithBonus
          score must be_== ( expectedScore )

          val minSliceSize = math.min( math.abs( gameManager.scorer.sliceRange.head ), gameManager.scorer.sliceRange.last )
          scoreSlice.size must be_>=( minSliceSize )
          scoreSlice.size must be_<( gameManager.scorer.sliceRange.size )
*/

        case _ => fail
      }

      users.foreach{
        user =>
        val DataPulled( Some( userHistory ) ) = gameUserHistoryService !? PullData( GameUserKey( game.id, user.id ) )
        val expectedHistory = game.questions.zipWithIndex.reverse.map{ case( q, i ) => AnswerHistory( i, user.id%(q.answers.size) ) }
        userHistory.asInstanceOf[GameUserHistory].anwsers must be_==( expectedHistory )
      }
*/

    }

  }
  
}

