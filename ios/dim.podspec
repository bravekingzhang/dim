#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'dim'
  s.version          = '0.1.13'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.ios.deployment_target = '8.0'
  s.vendored_frameworks = [
    'ImSDK.framework',
    'QALSDK.framework',
    'TLSSDK.framework',
    'IMFriendshipExt.framework',
    'IMGroupExt.framework',
    'IMMessageExt.framework',
    'IMSDKBugly.framework',
    'QALHttpSDK.framework'
  ]
end

