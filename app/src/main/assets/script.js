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

function toggleInfo(e) {
    if (info.classList.contains(COLLAPSED) || calcOverlap() >= OVERLAP_THRESHOLD)
        info.classList.toggle(COLLAPSED)
}

// available since ECMA6
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    }
}

// Is this page currently visible to user
var current = false

function setCurrent(v) {
    console.log("setCurrent1: " + v)
    if (current == v)
        return
    current = v
    console.log("setCurrent2: " + current)
    if (current)
        restartGif()
    else
        gif.style.visibility = 'hidden'
}

function gifSrc() {
    // Because `gif.src` returns current web page address instead
    return gif.getAttribute("src")
}

function restartGif(e) {
    var src = gifSrc()
    if (src == NULL)
        return
    console.log("restartGif: '" + src + "'")
    if (!src.endsWith('_'))
        gif.src += '?'
    gif.src += '_'
    gif.style.visibility = 'visible'
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

    if (current && path != NULL)
        gif.style.visibility = 'visible'
}

function setInfo(text) {
    console.log("setInfo: " + text)
    infoInner.innerHTML = text
    info.classList.remove(COLLAPSED)
    toggleInfo()
}

function calcOverlap() {
    var yInfo = info.offsetTop, hInfo = info.offsetHeight
    var rectGif = gif.getBoundingClientRect()
    var yGif = rectGif.top, wGif = rectGif.width, hGif = rectGif.height
    var minDimGif = Math.min(wGif, hGif)
    yGif = yGif + (hGif / 2) - (minDimGif / 2)
    var overlap = Math.round(Math.min(100, Math.max(0, yInfo + hInfo - yGif) / minDimGif * 100))
    console.log("overlap: " + overlap + "%")
    return overlap
}
