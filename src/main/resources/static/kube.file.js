$("#namespace").on('change', function () {
    var namespace = $(this).val();
    $("#pod").empty()
    $.getJSON("/pod?namespace=" + namespace, function (result) {
        for (i in result) {
            $('#pod').append($('<option>', {
                value: result[i],
                text: result[i]
            }));

        }
        $('#pod').selectpicker('refresh');
        $('#pod').selectpicker('render');
    });
    $("#container").empty()
    $('#container').selectpicker('refresh');
    $('#container').selectpicker('render');
    clear()
});

$("#pod").on('change', function () {
    var namespace = getById("namespace")
    var index = namespace.selectedIndex
    var pod = $(this).val();
    $("#container").empty()
    $.getJSON("/container?namespace=" + namespace.options[index].value + "&pod=" + pod, function (result) {
        for (i in result) {
            $('#container').append($('<option>', {
                value: result[i],
                text: result[i]
            }));
        }
        $('#container').selectpicker('refresh');
        $('#container').selectpicker('render');
    });
    clear()
});

$("#container").on('change', function () {
    clear()
    setTbody()
});

function setTbody() {
    var namespace = getSelectValueById("namespace")
    var pod = getSelectValueById("pod")
    var container = getSelectValueById("container")
    var path = $("#path").text()
    var tbody = document.getElementById('tbody');
    tbody.innerHTML = "";
    $.getJSON("/list/" + namespace + "/" + pod + "/" + container + "?path=" + path, function (result) {
        for (var i = 0; i < result.length; i++) {
            var row = getRow(result[i])
            tbody.appendChild(row)
        }
    });
}

function getRow(data) {
    var row = document.createElement('tr');
    //name
    var name = document.createElement('td');
    name.innerHTML = data.name;
    row.appendChild(name);
    //right
    var right = document.createElement('td');
    right.innerHTML = data.right;
    row.appendChild(right);
    //size
    var size = document.createElement('td');
    size.innerHTML = data.size;
    row.appendChild(size);
    //time
    var time = document.createElement('td');
    time.innerHTML = data.time;
    row.appendChild(time);
    // operation
    var operation = document.createElement('td');
    var namespace = getSelectValueById("namespace")
    var pod = getSelectValueById("pod")
    var container = getSelectValueById("container")
    var path = $("#path").text()
    var download_href = "/download/" + namespace + "/" + pod + "/" + container + "/" + data.name + "?type=" + data.type + "&path=" + path
    var html = '<a href=' + download_href + '>\n' +
        '<button type="button" class="btn btn-success btn-sm">Download</button>\n' +
        '</a>&nbsp;&nbsp;';
    if (data.type == false) {
        var func = 'openDir("' + data.name + '")'
        html += '<a href="javascript:void(0)" onclick=' + func + '>\n' +
            '<button type="button" class="btn btn-info btn-sm">&nbsp;&nbsp;Open&nbsp;&nbsp;</button>&nbsp;&nbsp;\n' +
            '</a>'
    }
    html += '<a data-name=' + data.name + ' data-toggle="modal" data-target="#deleteModal">\n' +
        '<button type="button" class="btn btn-danger btn-sm">&nbsp;Delete&nbsp;</button>\n' +
        '</a>'
    operation.innerHTML = html
    row.appendChild(operation);
    return row
}

function openDir(name) {
    var text = $("#path").text()
    var html = $("#path").html()
    var func = 'openPath("' + name + '")'
    html += '<a href="javascript:void(0)" onclick=' + func + '>' + name + '</a>/'
    getById("path").innerHTML = html
    setTbody()
}

function openPath(name) {
    if (name == "@#$") {
        getById("path").innerHTML = '<a href="javascript:void(0)" onclick="openPath(\'@#$\')">/</a>'
    } else {
        var html = $("#path").html()
        var index = html.lastIndexOf(name)
        var new_html = html.substr(0, index + name.length) + '</a>/'
        getById("path").innerHTML = new_html
    }
    setTbody()
}

function getSelectValueById(id) {
    var select = getById(id)
    var select_index = select.selectedIndex
    var value = select.options[select_index].value
    return value
}

$(document).on({
    dragleave: function (e) {
        e.preventDefault();
    },
    drop: function (e) {
        e.preventDefault();
    },
    dragenter: function (e) {
        e.preventDefault();
    },
    dragover: function (e) {
        e.preventDefault();
    }
});

var box = document.getElementById('file-area');
box.addEventListener("drop",
    function (e) {
        e.preventDefault();
        var fileList = e.dataTransfer.files;
        if (fileList.length == 0) {
            return false;
        }
        if (fileList.length > 1) {
            alert("only 1 file can be upload at a time.");
            return false;
        }
        if (!e.dataTransfer.items[0].webkitGetAsEntry().isFile) {
            alert("folder upload is not supported now.");
            return false;
        }
        var filesize = Math.floor((fileList[0].size) / 1048576);
        if (filesize > 100) {
            alert("file max size can't bigger than 100Mb.");
            return false;
        }
        //上传
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function (ev) {
            if (xhr.readyState === 4) {
                alert(xhr.responseText)
                // location.reload();
                getById("percent").innerHTML = ''
                setTbody();
            }
        }
        xhr.upload.onprogress = function (event) {
            if (event.lengthComputable) {
                var percent = Math.floor(event.loaded / event.total * 100);
                if (percent == 100) {
                    document.getElementById('percent').innerHTML = '<p><b>' + fileList[0].name + '</b> upload to server <b style="color: green;">100%</b>, waiting to upload to the pod...</p>';
                } else {
                    document.getElementById('percent').innerHTML = '<p><b>' + fileList[0].name + '</b> upload to server <b style="color: green;">' + Math.floor(percent) + '%</b></p>';
                }
            }
        };
        var namespace = getSelectValueById("namespace")
        var pod = getSelectValueById("pod")
        var container = getSelectValueById("container")
        var path = $("#path").text()
        var url = "/upload/" + namespace + "/" + pod + "/" + container + "?path=" + path
        xhr.open("post", url, true);
        xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        var fd = new FormData();
        fd.append('file', fileList[0]);
        xhr.send(fd);
    },
    false);


$(function () {
    $('#deleteModal').on('show.bs.modal', function (e) {
        var btn = $(e.relatedTarget);
        filename = btn.data("name");
        document.getElementById("delete-name").value = filename;
        document.getElementById("delete-p").innerText = filename;
    })
});

function create_folder() {
    var foldername = getById("folder-name").value;
    var url = "/newfolder/" + foldername;
    var namespace = getSelectValueById("namespace")
    var pod = getSelectValueById("pod")
    var container = getSelectValueById("container")
    var path = $("#path").text()
    var url = "/newfolder/" + namespace + "/" + pod + "/" + container + "/" + foldername + "?path=" + path
    $.ajax({
        type: "post",
        url: url,
        success: function () {
            // location.reload();
            $('#newFolderModal').modal('hide');
            setTbody();
        }
    })
}

function clear() {
    getById("path").innerHTML = '<a href="javascript:void(0)" onclick="openPath(\'@#$\')">/</a>'
    getById("percent").innerHTML = ''
    getById("tbody").innerHTML = ''
}

function delete_file() {
    var filename = getById("delete-name").value;
    var namespace = getSelectValueById("namespace")
    var pod = getSelectValueById("pod")
    var container = getSelectValueById("container")
    var path = $("#path").text()
    var url = "/delete/" + namespace + "/" + pod + "/" + container + "/" + filename + "?path=" + path
    $.ajax({
        type: "post",
        url: url,
        success: function () {
            // location.reload();
            $('#deleteModal').modal('hide');
            setTbody();
        }
    })
}

function getById(id) {
    return document.getElementById(id);
}