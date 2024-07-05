let globalUserId = '';
let globalVideoUrl = '';

document.addEventListener('DOMContentLoaded', (event) => {
    const urlParams = new URLSearchParams(window.location.search);
    const username = urlParams.get('username');
    globalUserId = urlParams.get('userId');

    document.getElementById('username').innerText = username;

    document.getElementById('videoUrl').addEventListener('input', () => {
        removeDynamicButtons();
    });
});

async function submitLink() {
    globalVideoUrl = document.getElementById('videoUrl').value;
    const userId = globalUserId;

    try {
        const response = await fetch('http://127.0.0.1:8100/api/service-api/init-request?url=' + encodeURIComponent(globalVideoUrl) + '&userId=' + encodeURIComponent(userId), {
            method: 'GET'
        });
        const result = await response.text();
        handleApiResponse(response.status, result);
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

function handleApiResponse(status, result) {
    const container = document.querySelector('.container');
    const formGroup = document.getElementById("form");
    removeDynamicButtons();

    if (status === 201 || status === 406) { // Created
        alert("Video downloading. You can wait or play directly")
        removeDynamicButtons();
        const repeatButton = document.getElementById("submit");
        repeatButton.innerHTML = "Repeat request";
        const youtubeButton = document.createElement('button');
        youtubeButton.innerText = 'YouTube Player';
        youtubeButton.onclick = () => {
            window.location.href = `youtubePlayer.html?videoUrl=${encodeURIComponent(globalVideoUrl)}&userId=${encodeURIComponent(globalUserId)}`;
        };
        formGroup.appendChild(youtubeButton);
    } else if (status === 302) { // Found
        alert("Video found and ready to play");
        const cacheButton = document.createElement('button');
        cacheButton.innerText = 'Cache Player';
        cacheButton.onclick = () => {
            window.location.href = `cachePlayer.html?videoUrl=${encodeURIComponent(globalVideoUrl)}`;
        };
        formGroup.appendChild(cacheButton);
        const youtubeButton = document.createElement('button');
        youtubeButton.innerText = 'YouTube Player';
        youtubeButton.onclick = () => {
            window.location.href = `youtubePlayer.html?videoUrl=${encodeURIComponent(globalVideoUrl)}&userId=${encodeURIComponent(globalUserId)}`;
        };
        formGroup.appendChild(youtubeButton);
    } else {
        alert('Ошибка: ' + result);
    }
}

function removeDynamicButtons() {
    const repeatButton = document.getElementById("submit");
    repeatButton.innerHTML = "Submit";
    const buttons = document.querySelectorAll('#form button:not([onclick="submitLink()"]):not([onclick="logout()"])');
    buttons.forEach(button => button.remove());
}

function logout() {
    window.location.href = 'index.html';
}
