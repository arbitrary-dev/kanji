idText = "text"

function clickHandler(e) {
    var t = e.target

    if (t.src.endsWith(".gif"))
        t.src += '?'

    t.src += 'a'
}

function init(path, kanji) {
    var img = document.createElement("img")
    img.src = "file://" + path + "/" + kanji + ".gif"
    img.onclick = clickHandler

    var txt = document.createElement("p")
    txt.id = idText
    txt.innerHTML = kanji

    document.body.appendChild(img)
    // TODO make it initially oneline with ellipsis, expand on click, but keep it selectable
    document.body.appendChild(txt)
}

function etymology(text) {
    var txt = document.getElementById(idText)
    txt.innerHTML += " &ndash; " + text
}
