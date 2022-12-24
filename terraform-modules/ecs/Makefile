.PHONY: Init plan apply destroy

init: main.tf
	terraform init

plan: main.tf
	terraform fmt
	terraform validate
	terraform plan

apply: main.tf
	terraform apply --auto-approve

destory: main.tf
	terraform destroy --auto-approve