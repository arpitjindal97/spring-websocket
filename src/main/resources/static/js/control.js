/*var va=new Element("td",{"class":"sendbthcell"}),
    wa=new Element("div",{"class":"sendbtnwrapper"}),
    xa=new Element("button",{"class":"sendbtn",disabled:!0,text:"Send"});

var input = new Element("textarea",{"class":"chatmsg"});
*/

$(function ()
{
    $("#send").click(function()
    {
        websocket.send(  $("#input").val() );
        $("#input").val("");
    });
    $("#new_button").click(function()
    {
        var value = document.getElementById("new_button").innerHTML;
        if(value == "New") {

            document.getElementById("input").value="";
            document.getElementById("output").innerHTML="";

            testWebSocket();

        }
        else
        {
            websocket.close();
            websocket=null;
            disconnected();
            writeToScreen("<p class='statuslog'>You have disconnected</p>");
        }
    })
});

function disableInputArea()
{
    $("[id='input']").attr('disabled', true);
    $("[id='send']").attr('disabled', true);
};

function enableInputArea()
{
    $("[id='input']").attr('disabled', false);
    $("[id='send']").attr('disabled', false);
};
function disconnected()
{
    disableInputArea();
    document.getElementById("new_button").innerHTML="New";
    $("[id='start_or_stop_wrapper']").attr('class', "disconnectbtnwrapper newbtn");

}