package io.kroom.api.user

import io.kroom.api.util.Privacy
import io.kroom.api.deezer.DataDeezerGenre
import com.github.t3hnar.bcrypt._


case class DataUserPrivacy(
                            email: Int,
                            location: Int,
                            friends: Int,
                            musicalPreferencesGenre: Int,
                          )

case class DataUser(
                     id: Int,
                     userName: String,
                     email: String,
                     emailIsconfirmed: Boolean,
                     passHash: Option[String],
                     location: Option[String],
                     token: Option[String],
                     tokenOutOfDate: Option[String],
                     privacy: DataUserPrivacy
                   )

class RepoUser(val dbh: DBUser) {

  def getById(id: Int): Option[DataUser] = {
    dbh.getById(id)
  }

  def getFriends(userId: Int): List[DataUser] = {
    dbh.getFriends(userId)
  }

  def getmMsicalPreferences(userId: Int): List[DataDeezerGenre] = {
    dbh.getmMsicalPreferences(userId)
  }

  // Mutation

  def signUp(userName: String, email: String, pass: String): Option[DataUser] = {
    // TODO verif send email
    // TODO verif userName, email, pass
    // TODO gen token, add in header ?
    dbh.addUserWithPass(userName, email, pass.bcrypt)
  }

  def authenticate(userName: String, pass: String): Option[DataUser] = {
    // TODO Token
    dbh.checkUserNamePass(userName, pass.bcrypt)
  }

  def addFriend(userId: Int, friendId: Int): Option[DataUser] = {
    dbh.addFriend(userId, friendId)
  }

  def delFriend(userId: Int, friendId: Int): Option[DataUser] = {
    dbh.delFriend(userId, friendId)
  }

  def addMusicalPreference(userId: Int, genreId: Int): Option[DataUser] = {
    dbh.addMusicalPreference(userId, genreId)
  }

  def delMusicalPreference(userId: Int, genreId: Int): Option[DataUser] = {
    dbh.delMusicalPreference(userId, genreId)
  }

  def updatePrivacy(
                     userId: Int,
                     email: Privacy.Value,
                     location: Privacy.Value,
                     friends: Privacy.Value,
                     musicalPreferencesGenre: Privacy.Value,
                   ): Option[DataUser] = {
    dbh.updatePrivacy(userId, DataUserPrivacy(
      Privacy.PrivacyToInt(email),
      Privacy.PrivacyToInt(location),
      Privacy.PrivacyToInt(friends),
      Privacy.PrivacyToInt(musicalPreferencesGenre),
    ))
  }

}
