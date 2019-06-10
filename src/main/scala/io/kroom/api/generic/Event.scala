package io.kroom.api.generic

trait Event {
  def id: String
  def version: Long
}
