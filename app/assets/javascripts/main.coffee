$ ->
  countDown()
  checkLocked()
  commandForm()

  $ '.confirm'
  .on 'click', ->
    return confirm $(this).attr('data-confirm-text')

iframeFollowScroll = (frame) ->
  frame.addClass('loading')

  timer = setInterval ->
    contents = frame.contents()
    contents.scrollTop contents.height()
    clearTimeout(timer) if not frame.hasClass('loading')
    return
  , 100

  frame.on 'load', ->
    frame.removeClass('loading')
    return
  return

countDown = ->
  elem = $ '.time-left'
  return if not elem.length
  seconds = + elem.attr 'data-seconds'
  setInterval ->
    seconds--
    location.reload() if seconds <= 0
    elem.html secondsToString(seconds)
    return
  , 1000
  return

pad02 = (num) ->
  ('0' + num).substr(-2)

secondsToString = (seconds) ->
  pad02(Math.floor(seconds/60)) + ':' + pad02(Math.floor(seconds%60))

checkLocked = ->
  elem = $('#lock-form')
  return if not elem.length
  checkUrl = elem.attr('action')
  lockUser = elem.attr('data-lock-user') or ""
  setInterval ->
    $.ajax checkUrl
    .success (res) ->
      if lockUser isnt res
        location.reload()
      return
    return
  , 10000
  return

commandForm = ->
  $ '.command-form'
  .on 'submit', (ev) ->
    $ '#command-log'
    .removeClass 'hidden'

    commandLog = $ '#command-log iframe'
    iframeFollowScroll(commandLog)

    commitLog = $ '#commit-log iframe'
    commandLog.on 'load', onload = ->
      commandLog.off 'load', onload
      commitLog.attr 'src', commitLog.attr 'src'
      return
    return
  return
