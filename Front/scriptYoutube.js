var videoUrl;
    var userId;

    document.addEventListener('DOMContentLoaded', (event) => {
        const urlParams = new URLSearchParams(window.location.search);
        videoUrl = urlParams.get('videoUrl');
        userId = urlParams.get('userId');

        const videoId = extractVideoId(videoUrl);
        if (videoId) {
            loadYouTubePlayer(videoId);
        } else {
            alert('Invalid YouTube URL');
        }
    });

    function extractVideoId(url) {
        const regex = /(?:https?:\/\/)?(?:www\.)?(?:youtube\.com\/(?:[^\/\n\s]+\/\S+\/|(?:v|e(?:mbed)?)\/|\S*?[?&]v=)|youtu\.be\/)([a-zA-Z0-9_-]{11})/;
        const matches = url.match(regex);
        return matches ? matches[1] : null;
    }

    function loadYouTubePlayer(videoId) {
        const tag = document.createElement('script');
        tag.src = "https://www.youtube.com/iframe_api";
        const firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

        window.onYouTubeIframeAPIReady = () => {
            new YT.Player('player', {
                height: '360',
                width: '640',
                videoId: videoId,
                events: {
                    'onReady': onPlayerReady,
                }
            });
        };
    }

    function onPlayerReady(event) {
        event.target.playVideo();
    }

    function goBack() {
        window.history.back();
    }