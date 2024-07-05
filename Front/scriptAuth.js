function toggleForm() {
    const authForm = document.getElementById('authForm');
    const signupForm = document.getElementById('signupForm');
    if (authForm.style.display === 'none') {
        authForm.style.display = 'block';
        signupForm.style.display = 'none';
    } else {
        authForm.style.display = 'none';
        signupForm.style.display = 'block';
    }
}

async function signup() {
    const name = document.getElementById('signupName').value;
    const pass = document.getElementById('signupPass').value;

    try {
        const response = await fetch('http://localhost:8100/api/service-api/signup?name=' + encodeURIComponent(name) + '&pass=' + encodeURIComponent(pass), {
            method: 'POST'
        });
        const result = await response.text();
        
        if (response.status === 400) { // BAD_REQUEST
            alert('Такой пользователь уже существует');
        } else if (response.status === 202) { // ACCEPTED
            alert('Регистрация успешна');
            toggleForm();
        } else {
            alert('Ошибка: ' + result);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function login() {
    const name = document.getElementById('authName').value;
    const pass = document.getElementById('authPass').value;

    try {
        const response = await fetch('http://localhost:8100/api/service-api/login?name=' + encodeURIComponent(name) + '&pass=' + encodeURIComponent(pass), {
            method: 'GET'
        });
        const result = await response.text();
        
        if (response.status === 401) { // UNAUTHORIZED
            alert('Неверные данные авторизации');
        } else if (response.status === 202) { // ACCEPTED
            window.location.href = `linkInsert.html?username=${encodeURIComponent(name)}&userId=${encodeURIComponent(result)}`;
        } else {
            alert('Ошибка: ' + result);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}
