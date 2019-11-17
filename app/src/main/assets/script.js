const COLLAPSED = 'collapsed'
const OVERLAP_THRESHOLD = 10

var gif = document.createElement('img')
gif.id = 'gif'

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

var kanji
function setKanji(kanji) {
    this.kanji = kanji
}

function log(what) {
    console.log("[" + kanji + "] " + what)
}

var infoCollapsed
function toggleInfo(e) {
    if (infoCollapsed || isOverlapping()) {
        info.classList.toggle(COLLAPSED)
        // Fixes the issue where .glue elements of the first P become invisible
        // once .collapsed was toggled on for parent (even if it was then toggled off).
        infoInner.innerHTML = infoInner.innerHTML
        infoCollapsed = infoCollapsed ? false : true;
    }
}

// available since ECMA6
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    }
}

// Is this page currently visible to user
var current
function setCurrent(v) {
    if (current == v)
        return
    if (v) {
        if (current == null)
            // Restart GIF only once
            restartGif()
        if (gifSrc())
            gif.style.visibility = 'visible'
        if (infoCollapsed == null)
            // Toggle if not yet
            toggleInfo()
    }
    current = v
}

function gifSrc() {
    // Because `gif.src` returns current web page address instead
    return gif.getAttribute("src")
}

function restartGif(e) {
    var src = gifSrc()
    if (!src)
        return
    if (!src.endsWith('_'))
        gif.src += '?'
    gif.src += '_'
}

var prevPath

function setGif(path) {
    log("setGif: " + path)
    if (path == '' || path == 'null')
        path = null
    if (path == prevPath)
        return
    prevPath = path
    gif.src = path
    gif.style.visibility = (current && path) ? 'visible' : 'hidden'
}

function setInfo(text) {
    log("setInfo: " + text)
    infoInner.innerHTML = text
    // Check to prevent overriding user toggle
    if (infoCollapsed == null)
        toggleInfo()
}

function isOverlapping() {
    log("Is overlaping? " + gifSrc() + " " + gif.style.visibility)
    if (!gifSrc() || gif.style.visibility == 'hidden')
        return false
    var yInfo = info.offsetTop, hInfo = info.offsetHeight
    var rectGif = gif.getBoundingClientRect()
    var yGif = rectGif.top, wGif = rectGif.width, hGif = rectGif.height
    var minDimGif = Math.min(wGif, hGif)
    yGif = yGif + (hGif / 2) - (minDimGif / 2)
    var overlap = Math.round(Math.min(100, Math.max(0, yInfo + hInfo - yGif) / minDimGif * 100))
    log("Overlap: " + overlap + "% (" + OVERLAP_THRESHOLD + "%)")
    return overlap >= OVERLAP_THRESHOLD
}
