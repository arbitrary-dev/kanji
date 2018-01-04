idText = "text"

function clickHandler(e) {
    var t = e.target
    if (!t.src.endsWith("_"))
        t.src += '?'
    t.src += '_'
}

function init(path, kanji) {
    var img = document.createElement("img")
    img.src = "file://" + path + "/" + kanji + ".gif"
    img.onclick = clickHandler

    var txt = document.createElement("p")
    txt.id = idText

    document.body.appendChild(img)
    // TODO make it initially oneline with ellipsis, expand on click, but keep it selectable
    document.body.appendChild(txt)

    setText(kanji)
}

function setText(text) {
    document.getElementById(idText).innerHTML = text
}
