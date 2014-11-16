package models

import java.io._
import org.eclipse.jgit.api._
import org.eclipse.jgit.transport.URIish
import scala.collection.JavaConversions._
import scala.sys.process.Process

case class Repo(name: String) {
  lazy val dir = WorkingDir.projectDir(name)
  lazy val git = Git.open(dir)

  def readmeFile: Option[File] = {
    val file = new File(dir, ".deploy/config/readme.html")
    if (file.isFile) { Some(file) } else { None }
  }

  val deployCommand = ".deploy/bin/deploy"

  lazy val defaultCheckoutCommand = {
    val f = new File("/tmp/checkout.sh")
    if (!f.isFile) {
      val out = new PrintWriter(new BufferedWriter(new FileWriter(f)))
      out.print(
        """#!/bin/bash -eux
          |git fetch --prune
          |git reset --hard $DEPLOY_COMMIT
          |git clean -fdx
          |git submodule sync
          |git submodule init
          |git submodule update --recursive
        """.stripMargin
      )
      out.close()
      f.setExecutable(true)
    }
    f.getCanonicalPath
  }

  lazy val checkoutCommand = {
    val checkoutScript = ".deploy/bin/checkout_overwrite"
    // if checkout_overwrite script exists
    if (new File(dir, checkoutScript).isFile) {
      checkoutScript
    } else {
      defaultCheckoutCommand
    }
  }

  def commits = {
    val commits = git.log.setMaxCount(20).call()
    commits.map { new Commit(git, _) }
  }
}

object Repo {
  def clone(url: String): Repo = {
    val uriish = new URIish(url)
    Process(Seq("git", "clone", url), WorkingDir.projectsDir).!
    Repo(uriish.getHumanishName)
  }
}
