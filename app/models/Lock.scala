package models

import play.api.Play.current
import play.api.cache.Cache

import org.joda.time.DateTime

case class Lock(project: Project, user: User, endTime: DateTime) {
  def save() = Cache.set("lock_" + project.name, this)
  def delete() = Cache.remove("lock_" + project.name)
}

object Lock {
  def fetch(project: Project) = {
    Cache.getAs[Lock]("lock_" + project.name)
  }

  def save(project: Project, user: User, endTime: DateTime) = {
    new Lock(project, user, endTime).save()
  }
}