document.addEventListener('DOMContentLoaded', async (event) => {
    const urlParams = new URLSearchParams(window.location.search);
    const videoUrl = urlParams.get('videoUrl');

    document.getElementById('videoUrl').innerText = videoUrl;

    console.log(videoUrl);

    try {
        const response = await fetch('http://localhost:8100/api/service-api/use-service?url=' + encodeURIComponent(videoUrl), {
            method: 'GET'
        });
        if (response.status === 302) {
            console.log(response);
            const videoSourceUrl = await response.text();
            document.getElementById('videoPlayer').src = videoSourceUrl;
        } else {
            alert('Failed to fetch video source URL.');
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
});

function goBack() {
    window.history.back();
}