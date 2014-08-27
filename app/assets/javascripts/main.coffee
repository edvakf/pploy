$ ->
  fetchReadme()
  countDown()

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

fetchReadme = ->
  readme = $ '#deploy-readme'
  $.ajax(readme.attr 'data-src')
  .success (res) ->
    readme
    .toggleClass 'hidden'
    .html res
    return

countDown = ->
  elem = $ '.time-left'
  return if not elem.length
  seconds = + elem.attr 'data-seconds'
  setInterval ->
    seconds--
    location.reload() if seconds <= 0
    elem.text secondsToString(seconds)
  , 1000

pad02 = (num) ->
  ('0' + num).substr(-2)

secondsToString = (seconds) ->
  pad02(Math.floor(seconds/60)) + ':' + pad02(Math.floor(seconds%60))
