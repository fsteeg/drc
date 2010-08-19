package de.uni_koeln.ub.drc.data

object Tables {
  // sample tables:
  import com.novocode.squery.combinator._
  import com.novocode.squery.combinator.TypeMapper._
  import com.novocode.squery.combinator.basic.{ BasicTable => Table }

  object SampleUsers extends Table[(Int, String, Option[String])]("users") {
    def id = column[Int]("id", O NotNull, O NotNull)
    def first = column[String]("first", O Default "NFN", O DBType "varchar(64)")
    def last = column[Option[String]]("last")
    def * = id ~ first ~ last
  }

  object Orders extends Table[(Int, Int, String, Boolean, Option[Boolean])]("orders") {
    def userID = column[Int]("userID", O NotNull)
    def orderID = column[Int]("orderID", O NotNull, O NotNull)
    def product = column[String]("product")
    def shipped = column[Boolean]("shipped", O Default false, O NotNull)
    def rebate = column[Option[Boolean]]("rebate", O Default Some(false))
    def * = userID ~ orderID ~ product ~ shipped ~ rebate
  }
  
  // page -< word -< modification >- User
  //                       |           |
  //                       ^           |
  //                     vote >--------| 
  
  object Pages extends Table[(String, java.sql.Blob)]("pages") {
    def id = column[String]("id", O PrimaryKey)
    def image = column[java.sql.Blob]("image")
    def * = id ~ image
  }
  
  object Words extends Table[(String, String, String, Int, Int, Int, Int)]("words") {
    def id = column[String]("id", O PrimaryKey)
    def pageId = column[String]("pageId") // 1 page to N words
    def original = column[String]("original")
    def x = column[Int]("x")
    def y = column[Int]("y")
    def width = column[Int]("width")
    def height = column[Int]("height")
    def * = id ~ pageId ~ original ~ x ~ y ~ width ~ height
  }
  
  object Modifications extends Table[(String, String, String, String, Int)]("modifications") {
    def id = column[String]("id", O PrimaryKey)
    def wordId = column[String]("wordId") // 1 word to N modifications
    def authorId = column[String]("authorId") // 1 author to N modifications
    def form = column[String]("form")
    def score = column[Int]("score")
    def * = id ~ wordId ~ authorId ~ form ~ score
  }
  
  object Users extends Table[(String)]("users") {
    def id = column[String]("id", O PrimaryKey)
    def * = id
  }
  
  object Votes extends Table[(String, Int, String)]("votes") { // junction entity for N modifications to M users
    def id = column[String]("id", O PrimaryKey)
    def modificationId = column[Int]("modificationId") // 1 modification to N votes
    def userId = column[String]("userId") // 1 user to N votes
    def * = id ~ modificationId ~ userId
  }
}