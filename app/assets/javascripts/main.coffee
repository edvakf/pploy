$ ->
  jsSetting()
  countDown()
  checkLocked()
  commandForm()
  submitButtonHack()
  checkoutBeforeDeploy()

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
  return

iframeStopFollowingScroll = (frame) ->
  frame.removeClass('loading')
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
    setTimeout disableAllButtons, 10

    if window.setting['use_web_socket']
      postWebSocket this, commandLog, ->
        reloadCommitLog()
        iframeStopFollowingScroll(commandLog)
        enableAllButtons()
        return
      ev.preventDefault()
    else
      commandLog.on 'load', onload = ->
        commandLog.off 'load', onload
        reloadCommitLog()
        iframeStopFollowingScroll(commandLog)
        enableAllButtons()
        return

    return
  return

reloadCommitLog = ->
  commitLog = $ '#commit-log iframe'
  commitLog.attr 'src', commitLog.attr 'src'
  return

disableAllButtons = ->
  $ 'button'
  .each (i, e) ->
    $(e).prop 'disabled', true
    return
  return

enableAllButtons = ->
  $ 'button'
  .each (i, e) ->
    $(e).prop 'disabled', false
    return
  return

postWebSocket = (form, frame, oncomplete) ->
  $(frame).contents().find('body').html('<pre></pre>')
  pre = $(frame).contents().find('pre')

  action = $(form).prop 'action'
  query = $(form).serialize()
  ws = new WebSocket action.replace(/^http(s)?:\/\//, "ws$1:") + '?' + query
  ws.onopen = (event) ->
    return
  ws.onmessage = (event) ->
    pre.append($('<span>').text(event.data))
    return
  ws.onerror = (event) ->
    oncomplete()
    return
  ws.onclose = (event) ->
    oncomplete()
    return
  return

# for form.serialize()
# http://stackoverflow.com/a/11271850
submitButtonHack = ->
  $('form :submit').on 'click', ->
    if $(this).attr 'name'
      $form = $(this).closest('form')
      $hidden = $('<input type=hidden>').attr({
        name: $(this).attr('name'),
        value: $(this).attr('value')
      })
      $form.append($hidden)
      setTimeout ->
        $hidden.remove()
        return
      , 10
    return
  return

# pass Application settings to JavaScript via DOM elements with classname "js-setting"
jsSetting = ->
  window.setting = {}
  $('.js-setting').each ->
    window.setting[$(this).attr('data-name')] = $(this).attr('data-value')
    return
  return

checkoutBeforeDeploy = ->
  $('.checkout-button').on 'click', ->
    $('.deploy-button').prop('disabled', false)