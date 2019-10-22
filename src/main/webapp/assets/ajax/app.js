/**
 * 
 */

$(document).ready(function () {
        $("#loginbtn").click(function () {
                $.ajax({
                        url: "dashboard",
                        method: "POST",
                        success: function(){
                        	alert("Login Success!!");
                        },
                        error:function(){
                        	alert("Login Fault!!");
                        }
                });
        });
 
       
});