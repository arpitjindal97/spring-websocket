/**
 * Created by arpit on 2/5/17.
 */
var wsUri = "ws://localhost:8080/chat";
var key;

function testWebSocket()
{
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
}

function onOpen(evt)
{
    document.getElementById("new_button").innerHTML="Stop";
    $("[id='start_or_stop_wrapper']").attr('class', "disconnectbtnwrapper");
}

function onClose(evt)
{
    disconnected();
}

function onMessage(evt)
{
    var str = evt.data;
    var index = str.indexOf("server: ");

    if(index == 0)
    {
        str = str.substring(8,str.length);
        if(new String(str) == "textarea disabled")
        {
            disableInputArea();
        }
        else if(new String(str) == "key is 0")
        {
            key=0;
        }
        else if(new String(str) == "key is 1")
        {
            key=1;
        }
        else if(new String(str) == "textarea enabled")
        {
            enableInputArea();
        }
        else if(new String(str) == "disconnected")
        {
            disconnected();
        }
        else if(new String(str) == "didt found")
        {
            setTimeout(give_me_stranger, 500);
        }
    }
    else
        writeToScreen(evt.data);
}

function onError(evt)
{
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}


function writeToScreen(message)
{
    $("#output").append("<div class='logitem'>"+message+"</div>");

    $("#logbox").scrollTop($("#logbox")[0].scrollHeight);
}

$(document).ready(function()
{
    console.log("document is ready");
    testWebSocket();

    document.getElementById("input").onkeydown = function(evt) {
        evt = evt || window.event;

        if (evt.keyCode == 13) {
            var value = document.getElementById("input").value;
            if(value != "")
            {
                websocket.send(value);
                document.getElementById("input").value="";
            }
            return false;
        }
        else
        {
            return true;
        }
    };

});

document.onkeydown = function(evt) {
    evt = evt || window.event;
    var isEscape = false;
    if ("key" in evt) {
        isEscape = (evt.key == "Escape" || evt.key == "Esc");
    } else {
        isEscape = (evt.keyCode == 27);
    }
    if (isEscape) {
        escape_button_pressed();
    }
};