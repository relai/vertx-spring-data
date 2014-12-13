$(function(){
	// Filter
	$("#filterButtons button").click(function(event){
        window.location.replace("tasks?filter=" + event.target.id);
	});
});