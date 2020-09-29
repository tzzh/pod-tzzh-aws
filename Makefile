.PHONY: repl-server test generate lint

repl-server:
	bb -cp gen/ --nrepl-server

test:
	bb -cp gen/ -e "(require '[clojure.test :as t] '[generate-test]) (let [{:keys [:fail :error]} (t/run-tests 'generate-test)] (System/exit (+ fail error)))"

generate:
	bb gen/generate.clj

lint:
	docker run -v "${PWD}":/home/ --rm borkdude/clj-kondo clj-kondo --lint /home/
