$ ->
  fetchReadme()

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
