package io.kroom.api.generic

trait Versioned {
  def id: String
  def version: Long
}
