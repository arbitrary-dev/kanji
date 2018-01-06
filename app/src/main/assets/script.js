var gif = document.createElement('img')
gif.id = 'gif'
var info = document.createElement('div')
info.id = 'info'

gif.onclick = restartGif
info.onclick = toggleInfo

document.addEventListener('DOMContentLoaded', function(event) {
    document.body.appendChild(gif)
    document.body.appendChild(info)
})

function toggleInfo(e) {
    // TODO add "Show more..." hint
    info.classList.toggle('expand')
}

// available since ECMA6
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    }
}

function restartGif(e) {
    if (!gif.src.endsWith('_'))
        gif.src += '?'
    gif.src += '_'
}

var prevPath

function setGif(path) {
    if (path == prevPath)
        return
    prevPath = path
    gif.src = path
    gif.style.visibility = 'visible'
}

function setInfo(text) {
    info.innerHTML = text
}
