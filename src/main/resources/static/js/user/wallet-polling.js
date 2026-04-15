document.addEventListener('DOMContentLoaded', function() {
    const initialBalance = window.USER_CONTEXT.initialBalance;
    const isQrVisible = window.USER_CONTEXT.isQrVisible;
    
    let pollingInterval;

    function checkBalance() {
        fetch('/user/api/balance')
            .then(response => response.json())
            .then(currentBalance => {
                if (currentBalance > initialBalance) {
                    clearInterval(pollingInterval);
                    const amountAdded = currentBalance - initialBalance;
                    
                    if (typeof Swal !== 'undefined') {
                        Swal.fire({
                            title: 'Thành công! 🎉',
                            text: 'Bạn đã nạp thành công ' + amountAdded.toLocaleString('vi-VN') + ' VNĐ vào ví cá nhân.',
                            icon: 'success',
                            confirmButtonText: 'Tuyệt vời',
                            confirmButtonColor: '#28a745',
                            allowOutsideClick: false
                        }).then((result) => {
                            if (result.isConfirmed) {
                                window.location.href = '/user/userInfor?tab=wallet';
                            }
                        });
                    } else {
                        alert("🎉 TING TING! Bạn đã nạp thành công " + amountAdded.toLocaleString('vi-VN') + " VNĐ!");
                        window.location.href = '/user/userInfor?tab=wallet';
                    }
                }
            })
            .catch(error => console.error('Lỗi khi kiểm tra số dư:', error));
    }

    if (isQrVisible) {
        pollingInterval = setInterval(checkBalance, 3000);
    }
});