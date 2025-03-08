declare module '@kangc/v-md-editor' {
  import { DefineComponent } from 'vue'
  const VueMarkdownEditor: DefineComponent<{
    modelValue?: string
    height?: string | number
    disabled?: boolean
    readonly?: boolean
    placeholder?: string
    onChange?: (text: string, html: string) => void
    onSave?: (text: string, html: string) => void
  }>
  export default VueMarkdownEditor
}

declare module '@kangc/v-md-editor/lib/theme/vuepress.js' {
  const theme: any
  export default theme
}

declare module '@kangc/v-md-editor/lib/preview' {
  import { DefineComponent } from 'vue'
  const VueMarkdownPreview: DefineComponent<{
    text?: string
    html?: string
    theme?: any
  }>
  export default VueMarkdownPreview
}

declare module '@kangc/v-md-editor/lib/style/base-editor.css' {
  const content: any
  export default content
}

declare module '@kangc/v-md-editor/lib/style/preview.css' {
  const content: any
  export default content
}

declare module '@kangc/v-md-editor/lib/theme/style/vuepress.css' {
  const content: any
  export default content
} 