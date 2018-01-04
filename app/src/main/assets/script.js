idImage = "image"
idText = "text"

function clickHandler(e) {
    var t = e.target
    if (!t.src.endsWith("_"))
        t.src += '?'
    t.src += '_'
}

function init(kanji) {
    var img = document.createElement("img")
    img.id = idImage
    img.onclick = clickHandler

    var txt = document.createElement("p")
    txt.id = idText

    document.body.appendChild(img)
    // TODO make it initially oneline with ellipsis, expand on click, but keep it selectable
    document.body.appendChild(txt)

    setText(kanji)
}

function setGif(path) {
    var img = document.getElementById(idImage)
    img.src = path
    img.style.visibility = "visible"
}

function setText(text) {
    document.getElementById(idText).innerHTML = text
}
