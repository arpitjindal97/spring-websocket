/**
 * Created by arpit on 2/5/17.
 */
var wsUri = "ws://localhost:8080/marco";
var output;

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
    //writeToScreen("CONNECTED");
    document.getElementById("new_button").innerHTML="Stop";
    $("[id='start_or_stop_wrapper']").attr('class', "disconnectbtnwrapper");
}

function onClose(evt)
{
    //writeToScreen("<p class='statuslog'>Stranger has Disconnected</p>");
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
        else if(new String(str) == "textarea enabled")
        {
            enableInputArea();
        }
        else if(new String(str) == "disconnected")
        {
            disconnected();
        }
    }
    else
        writeToScreen(evt.data);
}

function onError(evt)
{
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function doSend(message)
{
    websocket.send(message);
}

function writeToScreen(message)
{
    $("#output").append("<div class='logitem'>"+message+"</div>")
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

