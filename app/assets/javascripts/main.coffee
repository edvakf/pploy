$ ->
  $ '#deploy-form'
  .on 'submit', (ev) ->
    $ '#deploy-log'
    .show()
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
