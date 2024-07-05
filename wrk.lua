local names = {"user1", "user2", "user3", "user4", "user5", "user6", "user7", "user8", "user9", "user10"}
local passwords = {"pass1", "pass2", "pass3", "pass4", "pass5", "pass6", "pass7", "pass8", "pass9", "pass10"}

function random_credentials()
    local index = math.random(#names)
    return names[index], passwords[index]
end


function random_video_url()
    local urls = {
        "https://www.youtube.com/watch?v=uodTzgmw8_c",
        "https://www.youtube.com/watch?v=Sg_5QaNJ9bo",
        "https://www.youtube.com/watch?v=WvNeXbE_Ltg"
    }
    return urls[math.random(#urls)]
end

wrk.method = "GET"
wrk.headers["Content-Type"] = "application/json"

-- Запросы
request_login = function()
    local name, pass = random_credentials()
    return wrk.format("GET", "http://localhost:8100/api/service-api/login?name=" .. name .. "&pass=" .. pass)
end

request_signup = function()
    local name, pass = random_credentials()
    return wrk.format("POST", "http://localhost:8100/api/service-api/signup?name=" .. name .. "&pass=" .. pass)
end

request_use_service = function()
    local url = random_video_url()
    return wrk.format("GET", "http://localhost:8100/api/service-api/use-service?url=" .. url)
end

request_init_request = function()
    local url = random_video_url()
    return wrk.format("GET", "http://localhost:8100/api/service-api/init-request?url=" .. url .. "&userId=-999")
end

request = function()
    local requests = {
        request_login,
        request_signup,
        request_use_service,
        request_init_request
    }
    return requests[math.random(#requests)]()
end
