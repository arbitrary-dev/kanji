const NULL = 'null'
const COLLAPSED = 'collapsed'
const OVERLAP_THRESHOLD = 10

var gif = document.createElement('img')
gif.id = 'gif'
gif.src = NULL

var info = document.createElement('div')
info.id = 'info'

var infoInner = document.createElement('div')
infoInner.id = 'infoInner'
info.appendChild(infoInner)

var more = document.createElement('div')
more.id = 'more'
more.textContent = 'Show more'

info.appendChild(more)

gif.onclick = restartGif
info.onclick = toggleInfo

document.addEventListener('DOMContentLoaded', function(event) {
    document.body.appendChild(gif)
    document.body.appendChild(info)
})

var infoToggled = false
function toggleInfo(e) {
    if (info.classList.contains(COLLAPSED) || isOverlapping()) {
        info.classList.toggle(COLLAPSED)
        // Fixes the issue where .glue elements of the first P become invisible
        // once .collapsed was toggled on for parent (even if it was then toggled off).
        infoInner.innerHTML = infoInner.innerHTML
        // Toggling counts only when user did it, that's why we check event here.
        if (e) infoToggled = true;
    }
}

// available since ECMA6
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    }
}

// Is this page currently visible to user
var current = NULL

function setCurrent(v) {
    if (current == v)
        return
    if (v) {
        if (current == NULL)
            // Restart GIF only once
            restartGif()
        gif.style.visibility = 'visible'
    }
    current = v
}

function gifSrc() {
    // Because `gif.src` returns current web page address instead
    return gif.getAttribute("src")
}

function restartGif(e) {
    var src = gifSrc()
    console.log("restartGif: '" + src + "'")
    if (src == NULL)
        return
    if (!src.endsWith('_'))
        gif.src += '?'
    gif.src += '_'
}

var prevPath

function setGif(path) {
    if (path == '')
        path = NULL
    if (path == prevPath)
        return
    console.log("setGif: " + path)
    prevPath = path
    gif.src = path

    gif.style.visibility = (current && path != NULL) ? 'visible' : 'hidden'
}

function setInfo(text) {
    console.log("setInfo: " + text)
    infoInner.innerHTML = text
    // Check to prevent overriding user toggle.
    if (!infoToggled) {
        info.classList.remove(COLLAPSED)
        toggleInfo()
    }
}

function isOverlapping() {
    if (gifSrc() == NULL || gif.style.visibility == 'hidden')
        return false
    var yInfo = info.offsetTop, hInfo = info.offsetHeight
    var rectGif = gif.getBoundingClientRect()
    var yGif = rectGif.top, wGif = rectGif.width, hGif = rectGif.height
    var minDimGif = Math.min(wGif, hGif)
    yGif = yGif + (hGif / 2) - (minDimGif / 2)
    var overlap = Math.round(Math.min(100, Math.max(0, yInfo + hInfo - yGif) / minDimGif * 100))
    console.log("overlap: " + overlap + "%")
    return overlap >= OVERLAP_THRESHOLD
}
