deploy:
	git add -A .
	git commit -a --amend -m-
	git push --force heroku master
	git push --force
