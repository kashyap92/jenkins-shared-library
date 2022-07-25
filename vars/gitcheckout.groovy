def call(Map StageParams){
    checkout(
        [
            $class: 'GitSCM',
            branches: [[name: StageParams.branch]], 
            extensions: [], 
            userRemoteConfigs: 
            [
                [
                    url: StageParams.url
                ]
            ]
        ])
}