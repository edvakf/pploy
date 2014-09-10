$ ->
  countDown()
  checkLocked()

  $ '.confirm'
  .on 'click', ->
    return confirm $(this).attr('data-confirm-text')

  $ '.command-form'
  .on 'submit', (ev) ->
    $ '#command-log'
    .removeClass 'hidden'
    iframeFollowScroll($ '#command-log iframe')
    return
  return

iframeFollowScroll = (frame) ->
  flag = false

  timer = setInterval ->
    contents = frame.contents()
    contents.scrollTop contents.height()
    clearTimeout(timer) if flag
    return
  , 100

  frame.on 'load', ->
    flag = true
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
  elem = $('[name="operation"][value="gain"]')
  return if not elem.length
  setInterval ->
    $.ajax elem.closest('form').attr('action')
    .success (res) ->
      location.reload() if res is not ""
      return
    return
  , 10000
  return
