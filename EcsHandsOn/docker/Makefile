.PHONY: build login push delete rm

repository_name = test-ecs
image_name = hello-world

build: Dockerfile
	docker build -t $(image_name) .
	docker tag $(image_name) $(AWS_ACCOUNT_ID).dkr.ecr.$(AWS_REGION).amazonaws.com/$(repository_name)
	
	# echo $(AWS_ACCOUNT_ID)
	# echo $(AWS_REGION)

login: 
	aws ecr get-login-password --region $(AWS_REGION) | docker login --username AWS --password-stdin $(AWS_ACCOUNT_ID).dkr.ecr.$(AWS_REGION).amazonaws.com
	
push: 
	docker push $(AWS_ACCOUNT_ID).dkr.ecr.$(AWS_REGION).amazonaws.com/$(repository_name)
	
delete:
	aws ecr delete-repository --repository-name $(repository_name) --region $(AWS_REGION) --force
	
rm:
	docker rmi $(image_name)
	docker rmi $(AWS_ACCOUNT_ID).dkr.ecr.$(AWS_REGION).amazonaws.com/$(repository_name)