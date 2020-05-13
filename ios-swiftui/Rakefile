# frozen_string_literal

ENV['MINT_PATH'] = './Mint'

desc 'Generate and run the OpenTrace app'
task :opentrace do 
  install_packages
  generate_opentrace_project
  sh %(open OpenTrace.xcodeproj)
end

desc 'Run Swiftgen'
task :swiftgen do
  sh %(mint run swiftgen swiftgen)
end

desc 'Run Swiftlint'
task :swiftlint do
  sh %(mint run swiftlint)
end

desc 'Run Swiftformat'
task :swiftformat do
  sh %(mint run swiftformat swiftformat . --swiftversion 5.2)
end

def generate_opentrace_project
  sh %(mint run xcodegen xcodegen --spec openTraceProject.yml --use-cache)
end

def install_packages
  sh %(
    mkdir Mint
    mint bootstrap
  )
end

def install_gems 
  sh %(
    if ! cmp -s Gemfile.lock vendor/Gemfile.lock; then 
      bundle install --path vendor/bundle
      cp Gemfile.lock vendor
    fi
  )
end
