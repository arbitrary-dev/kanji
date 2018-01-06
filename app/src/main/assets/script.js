idImage = "image"
idText = "text"

function clickHandlerP(e) {
    var t = e.target
    t.classList.toggle("expand")
}

function clickHandlerImg(e) {
    var t = e.target
    if (!t.src.endsWith("_"))
        t.src += '?'
    t.src += '_'
}

function init(kanji) {
    var img = document.createElement("img")
    img.id = idImage
    img.onclick = clickHandlerImg

    var txt = document.createElement("p")
    txt.id = idText
    txt.onclick = clickHandlerP

    document.body.appendChild(img)
    document.body.appendChild(txt)

    setText(kanji)
}

var prevPath

function setGif(path) {
    var img = document.getElementById(idImage)
    if (path == prevPath)
        return
    prevPath = path
    img.src = path
    img.style.visibility = "visible"
}

function setText(text) {
    document.getElementById(idText).innerHTML = text
}
