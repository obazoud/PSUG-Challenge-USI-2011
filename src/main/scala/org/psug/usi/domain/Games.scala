package org.psug.usi.domain

import org.psug.usi.store.{BDBDataRepository, InMemoryDataRepository, Data}

/**
 * User: alag
 * Date: 2/16/11
 * Time: 11:09 PM
 */

case class Game( id : Int=0, questions:Seq[Question], timeoutSec:Int=0, numPlayer:Int=0 ) extends Data[Int]{
  def storeKey:Int = id
  def copyWithAutoGeneratedId( id:Int ) = Game( id, questions, timeoutSec, numPlayer )
}

case class Question( question:String, answers:Seq[Answer], value:Int )

case class Answer( anwser:String, status:Boolean )


class GameRepository extends BDBDataRepository[Game]( "GameRepository" )
