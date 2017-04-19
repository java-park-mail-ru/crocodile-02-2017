var stompClient = null;
var gameId = 0;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/sp-games/');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        //console.log('Connected: ' + frame);
        stompClient.subscribe('/ws/sp-create/', function (inc) {
            //console.log( inc.body );
            //console.log( JSON.parse( inc.body ).gameId);
            gameId = JSON.parse(inc.body).gameId;
            showGreeting(JSON.parse(inc.body).gameId);
        });
    });
}

function start() {

    stompClient.subscribe('/ws/sp-game/' + gameId, function (result) {
        showGreeting(JSON.parse(result.body).correct);
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/ws/sp-answer/" + gameId, {}, JSON.stringify({'word': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#start").click(function () {
        start();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#send").click(function () {
        sendName();
    });
});