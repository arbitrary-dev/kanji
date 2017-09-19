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
    txt.innerHTML = kanji

    document.body.appendChild(txt)
    document.body.appendChild(img)
}
