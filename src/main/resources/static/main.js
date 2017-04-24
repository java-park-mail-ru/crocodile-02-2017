"use strict";

var address = document.getElementById("address");
var type = document.getElementById("type");
var content = document.getElementById("content");

const connect_button = document.getElementById("connect");
const disconnect_button = document.getElementById("disconnect");
const send_button = document.getElementById("send");
var ws;

connect_button.addEventListener("click", function () {
    ws = new WebSocket(address.value);

    ws.onopen = function () {
        console.log("CONNECTION");

        connect_button.disabled = true;
        disconnect_button.disabled = false;
        send_button.disabled = false;
        /*this.interval = setInterval(function () {
         ws.send(JSON.stringify({type:0, content: "update"}));
         }, 20000);*/

        ws.onmessage = function (event) {
            var message = JSON.parse(event.data);

            var newMes = document.createElement("p");
            document.body.appendChild(newMes);

            newMes.innerHTML = "Type: " + message.type + "     Content: " + JSON.stringify(message.content).slice(0, 100);
            console.log("RECEIVE TYPE: " + message.type, " CONTENT: " + JSON.stringify(message.content).slice(0, 100));
        };
    };
});

disconnect_button.addEventListener("click", function () {
    ws.close();
    ws.onclose = function () {
        console.log("DISCONNECTION");
        clearInterval(this.interval);
    };
    connect_button.disabled = false;
    disconnect_button.disabled = true;
    send_button.disabled = true;
});

send_button.addEventListener("click", function () {
    ws.send(JSON.stringify({type: type.value, content: {"answer": content.value}}));
    console.log("SEND TYPE: " + type.value, " CONTENT: " + content.value);
});