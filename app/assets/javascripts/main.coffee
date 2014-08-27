$ ->
  fetchReadme()
  countDown()
  checkLocked()

  $ '#deploy-form'
  .on 'submit', (ev) ->
    $ '#deploy-log'
    .toggleClass 'hidden'
    iframeFollowScroll($ '#deploy-log iframe')
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

fetchReadme = ->
  readme = $ '#deploy-readme'
  $.ajax(readme.attr 'data-src')
  .success (res) ->
    readme
    .toggleClass 'hidden'
    .html res
    return
  return

countDown = ->
  elem = $ '.time-left'
  return if not elem.length
  seconds = + elem.attr 'data-seconds'
  setInterval ->
    seconds--
    location.reload() if seconds <= 0
    elem.text secondsToString(seconds)
    return
  , 1000
  return

pad02 = (num) ->
  ('0' + num).substr(-2)
  return

secondsToString = (seconds) ->
  pad02(Math.floor(seconds/60)) + ':' + pad02(Math.floor(seconds%60))
  return

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
